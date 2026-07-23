import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, NavigationEnd, Router, RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { filter, map, startWith } from 'rxjs';

export type BreadcrumbItem = {
  label: string;
  url: string | null;
};

type BreadcrumbParent = {
  label: string;
  url: string;
};

const DASHBOARD_CRUMB: BreadcrumbItem[] = [{ label: 'Dashboard', url: null }];

@Component({
  selector: 'app-breadcrumbs',
  standalone: true,
  imports: [RouterLink, MatIconModule],
  templateUrl: './breadcrumbs.html',
  styleUrl: './breadcrumbs.scss'
})
export class Breadcrumbs {
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);

  readonly crumbs = toSignal(
    this.router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd),
      startWith(null),
      map(() => this.buildCrumbs())
    ),
    // Safe default — do not call buildCrumbs() here (router tree may not be ready).
    { initialValue: DASHBOARD_CRUMB }
  );

  private buildCrumbs(): BreadcrumbItem[] {
    try {
      const leaf = this.leafRoute();
      if (!leaf) {
        return DASHBOARD_CRUMB;
      }

      const data = leaf.snapshot?.data ?? {};
      const label = this.resolveLabel(leaf);
      if (!label) {
        return DASHBOARD_CRUMB;
      }

      const parents = (data['breadcrumbParents'] as BreadcrumbParent[] | undefined) ?? [];
      const currentUrl = this.urlOf(leaf);

      const items: BreadcrumbItem[] = [
        { label: 'Dashboard', url: '/app/dashboard' },
        ...parents.map((p) => ({ label: p.label, url: p.url })),
        { label, url: currentUrl }
      ];

      // Dedupe consecutive identical URLs (e.g. Dashboard when already there)
      const deduped: BreadcrumbItem[] = [];
      for (const item of items) {
        const prev = deduped[deduped.length - 1];
        if (prev && prev.url === item.url && prev.label === item.label) {
          continue;
        }
        deduped.push(item);
      }

      // If only Dashboard, mark current
      if (deduped.length === 1 && deduped[0].url === '/app/dashboard') {
        return DASHBOARD_CRUMB;
      }

      return deduped.map((item, index) =>
        index === deduped.length - 1 ? { ...item, url: null } : item
      );
    } catch {
      // Never blank the shell if route snapshots are incomplete after login.
      return DASHBOARD_CRUMB;
    }
  }

  private leafRoute(): ActivatedRoute | null {
    let route: ActivatedRoute | null = this.activatedRoute.root;
    while (route?.firstChild) {
      route = route.firstChild;
    }
    return route;
  }

  private urlOf(route: ActivatedRoute): string {
    const url = route.pathFromRoot
      .map((r) => {
        const segments = r.snapshot?.url;
        if (!segments?.length) {
          return '';
        }
        return segments.map((s) => s.path).join('/');
      })
      .filter(Boolean)
      .join('/');
    if (!url) {
      return '/app/dashboard';
    }
    return url.startsWith('app') ? `/${url}` : `/app/${url}`;
  }

  private resolveLabel(route: ActivatedRoute): string | null {
    const configured = route.snapshot?.data?.['breadcrumb'] as string | undefined;
    if (!configured) {
      return null;
    }

    const path = route.snapshot?.routeConfig?.path ?? '';
    if (path.includes(':id')) {
      const id = route.snapshot?.paramMap?.get('id');
      return id ? `${configured} ${id}` : configured;
    }

    return configured;
  }
}

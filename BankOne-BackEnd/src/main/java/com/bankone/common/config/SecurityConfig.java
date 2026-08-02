package com.bankone.common.config;

import com.bankone.auth.security.CustomUserDetailsService;
import com.bankone.auth.security.JwtAccessDeniedHandler;
import com.bankone.auth.security.JwtAuthenticationEntryPoint;
import com.bankone.auth.security.JwtAuthenticationFilter;
import com.bankone.ratelimit.RateLimitFilter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;
    private final ObjectProvider<RateLimitFilter> rateLimitFilterProvider;

    public SecurityConfig(
            CustomUserDetailsService userDetailsService,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint authenticationEntryPoint,
            JwtAccessDeniedHandler accessDeniedHandler,
            ObjectProvider<RateLimitFilter> rateLimitFilterProvider
    ) {
        this.customUserDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.rateLimitFilterProvider = rateLimitFilterProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/login").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/customers/**")
                        .hasAuthority("ACCESS_CUSTOMERS_READ")
                        .requestMatchers(HttpMethod.POST, "/customers", "/customers/**")
                        .hasAuthority("ACCESS_CUSTOMERS_WRITE")
                        .requestMatchers(HttpMethod.PUT, "/customers/**")
                        .hasAuthority("ACCESS_CUSTOMERS_WRITE")
                        .requestMatchers(HttpMethod.DELETE, "/customers/**")
                        .hasAuthority("ACCESS_CUSTOMERS_DELETE")
                        .requestMatchers(HttpMethod.GET, "/accounts/**")
                        .hasAuthority("ACCESS_ACCOUNTS_READ")
                        .requestMatchers(HttpMethod.GET, "/transactions/**")
                        .hasAuthority("ACCESS_ACCOUNTS_READ")
                        .requestMatchers(HttpMethod.GET, "/reports/approvals", "/reports/approvals/**")
                        .hasAnyRole("ADMIN", "MANAGER", "AUDITOR")
                        .requestMatchers(HttpMethod.GET, "/reports/**")
                        .hasAuthority("ACCESS_ACCOUNTS_READ")
                        .requestMatchers(HttpMethod.POST, "/accounts", "/accounts/**")
                        .hasAuthority("ACCESS_ACCOUNTS_WRITE")
                        .requestMatchers(HttpMethod.PUT, "/accounts/**")
                        .hasAuthority("ACCESS_ACCOUNTS_WRITE")
                        .requestMatchers("/users/**")
                        .hasAuthority("ACCESS_USERS_MANAGE")
                        .requestMatchers("/roles/**")
                        .hasAnyAuthority("ACCESS_ROLES_MANAGE", "ACCESS_USERS_MANAGE")
                        .requestMatchers("/portal/**")
                        .hasAuthority("ACCESS_PORTAL_ACCOUNTS")
                        .requestMatchers("/audit/**")
                        .hasAnyRole("ADMIN", "MANAGER", "AUDITOR")
                        .requestMatchers("/admin/replica/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/lab/shards/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/transfer-approvals/**")
                        .hasAuthority("ACCESS_ACCOUNTS_WRITE")
                        .requestMatchers(HttpMethod.GET, "/account-policies/**")
                        .hasAnyAuthority(
                                "ACCESS_POLICIES_MANAGE",
                                "ACCESS_ACCOUNTS_WRITE",
                                "ACCESS_ACCOUNTS_READ"
                        )
                        .requestMatchers("/account-policies/**")
                        .hasAuthority("ACCESS_POLICIES_MANAGE")
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .httpBasic(httpBasic -> httpBasic.disable());

        // Rate limit first (optional — absent when Redis rate-limit is off).
        RateLimitFilter rateLimitFilter = rateLimitFilterProvider.getIfAvailable();
        if (rateLimitFilter != null) {
            http.addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class);
        }
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Prevent Boot from also registering the JWT filter as a servlet filter.
     * Double registration can clear SecurityContext before authorization runs.
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(
            JwtAuthenticationFilter filter
    ) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration =
                new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    /**
     * Same idea for rate limit: only run inside the Security filter chain,
     * otherwise Boot would register the @Component filter twice (2 tokens / request).
     */
    @Bean
    @ConditionalOnBean(RateLimitFilter.class)
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(RateLimitFilter filter) {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origin-patterns}") String allowedOriginPatterns
    ) {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> patterns = Arrays.stream(allowedOriginPatterns.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        configuration.setAllowedOriginPatterns(patterns);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of(
                "X-RateLimit-Remaining",
                "Retry-After",
                "X-BankOne-Shard"
        ));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

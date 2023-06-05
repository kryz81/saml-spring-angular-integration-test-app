package kryz.samlspringangularintegrationtestapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {
    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return new CustomLogoutSuccessHandler();
    }

    @Bean
    SecurityFilterChain configure(HttpSecurity http) throws Exception {
        OpenSaml4AuthenticationProvider authenticationProvider = new OpenSaml4AuthenticationProvider();
        authenticationProvider.setResponseAuthenticationConverter(groupsConverter());


        http.cors().and().authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/authenticated").permitAll()
                        .anyRequest().authenticated())
                .logout()
                //.clearAuthentication(true)
                .logoutSuccessHandler(logoutSuccessHandler())
                //.deleteCookies("JSESSIONID")
                //.invalidateHttpSession(true)
                //.permitAll()
                .and()
                .csrf().disable()
                .saml2Login(saml2 -> saml2
                        .authenticationManager(
                                new ProviderManager(authenticationProvider)
                        ).defaultSuccessUrl("http://localhost:4200?auth=true", true)
                )
                .saml2Logout(withDefaults());
        return http.build();
    }

    private Converter<OpenSaml4AuthenticationProvider.ResponseToken, Saml2Authentication> groupsConverter() {

        Converter<OpenSaml4AuthenticationProvider.ResponseToken, Saml2Authentication> delegate =
                OpenSaml4AuthenticationProvider.createDefaultResponseAuthenticationConverter();

        return (responseToken) -> {
            Saml2Authentication authentication = delegate.convert(responseToken);
            Saml2AuthenticatedPrincipal principal = (Saml2AuthenticatedPrincipal) authentication.getPrincipal();
            List<String> groups = principal.getAttribute("groups");
            Set<GrantedAuthority> authorities = new HashSet<>();
            if (groups != null) {
                groups.stream().map(SimpleGrantedAuthority::new).forEach(authorities::add);
            } else {
                authorities.addAll(authentication.getAuthorities());
            }
            return new Saml2Authentication(principal, authentication.getSaml2Response(), authorities);
        };
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("null", "http://localhost:4200", "http://localhost:9090", "https://mocksaml.com"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

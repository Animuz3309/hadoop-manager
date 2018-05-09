package edu.scut.cs.hm.admin.web.api;

import edu.scut.cs.hm.admin.config.configurer.TokenServiceConfigurer;
import edu.scut.cs.hm.admin.filter.TokenAuthenticationFilter;
import edu.scut.cs.hm.admin.web.model.token.UITokenData;
import edu.scut.cs.hm.admin.web.model.token.UiUserCredentials;
import edu.scut.cs.hm.common.security.token.TokenConfiguration;
import edu.scut.cs.hm.common.security.token.TokenData;
import edu.scut.cs.hm.common.security.token.TokenService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping(value = "/api/token", produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TokenApi {

    private final TokenService tokenService;
    private final AuthenticationProvider authenticationProvider;
    private final TokenServiceConfigurer tokenServiceConfigurer;

    @ApiOperation("User header name: " + TokenAuthenticationFilter.X_AUTH_TOKEN)
    @RequestMapping(value = "login", method = POST)
    public UITokenData getToken(@RequestBody UiUserCredentials credentials) {
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(credentials.getUsername(), credentials.getPassword());
        final Authentication authenticate = authenticationProvider.authenticate(authentication);
        if (authenticate != null && authenticate.isAuthenticated()) {
            return createToken(credentials.getUsername());
        } else {
            throw new BadCredentialsException("Invalid login and password");
        }
    }

    @RequestMapping(value = "refresh", method = RequestMethod.PUT)
    public UITokenData refresh() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName(); //get logged in username
        return createToken(name);
    }

    @ApiOperation("Token should be passed it via " + TokenAuthenticationFilter.X_AUTH_TOKEN)
    @RequestMapping(value = "info", method = RequestMethod.GET)
    public UITokenData info(@RequestHeader(value= TokenAuthenticationFilter.X_AUTH_TOKEN) String token) {
        Assert.notNull(token, "token is null pass it via " + TokenAuthenticationFilter.X_AUTH_TOKEN);
        TokenData tokendata = tokenService.getToken(token);

        return fillFields(tokendata);
    }

    private UITokenData createToken(String name) {
        TokenConfiguration tokenConfiguration = new TokenConfiguration();
        tokenConfiguration.setUsername(name);
        TokenData token = tokenService.createToken(tokenConfiguration);
        return fillFields(token);
    }

    private UITokenData fillFields(TokenData token) {
        Instant instant = Instant.ofEpochMilli(token.getCreationTime());
        return UITokenData.builder()
                .creationTime(LocalDateTime.ofInstant(instant, ZoneOffset.UTC))
                .expireAtTime(LocalDateTime.ofInstant(instant.plusSeconds(tokenServiceConfigurer.getExpireAfterInSec()), ZoneOffset.UTC))
                .key(token.getKey())
                .username(token.getUsername())
                .build();
    }
}

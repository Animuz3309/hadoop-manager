package edu.scut.cs.hm.common.security;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.scut.cs.hm.common.json.JacksonUtils;
import lombok.Data;
import org.junit.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class JsonImmutableSecurityObjectTest {
    private static final ObjectMapper mapper = JacksonUtils.objectMapperBuilder();

    @Test
    public void testMap2Json() throws Exception {
        /* same as @JsonInclude(value = NON_EMPTY, content = NON_NULL) */
        /* see javadoc in @JsonInclude */
        JsonInclude.Value v = JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.NON_NULL);
        mapper.configOverride(java.util.Map.class).setInclude(v);

        MockBean mockBean = new MockBean();
        assertEquals("{}", mapper.writeValueAsString(mockBean));
        mockBean.prop = new HashMap<>();
        assertEquals("{}", mapper.writeValueAsString(mockBean));
        mockBean.prop.put(1, "1");
        mockBean.prop.put(2, null);
        assertEquals("{\"prop\":{\"1\":\"1\"}}", mapper.writeValueAsString(mockBean));
    }

    @Test
    public void testDate2Json() throws Exception {
        MockBean mockBean = new MockBean();
        mockBean.date = new Date();

        DateFormat dateFormat = new SimpleDateFormat(JacksonUtils.JSON_DATETIME_FORMAT_PATTERN);
        dateFormat.setTimeZone(JacksonUtils.DEFAULT_TIMEZONE);
        DateTimeFormatter localDateTimeFormat = DateTimeFormatter.ofPattern(JacksonUtils.JSON_DATETIME_FORMAT_PATTERN);

        assertEquals(String.format("{\"date\":\"%s\"}", dateFormat.format(mockBean.date)),
                mapper.writeValueAsString(mockBean));

        mockBean.localDateTime = LocalDateTime.now();
        assertEquals(String.format("{\"date\":\"%s\",\"localDateTime\":\"%s\"}",
                dateFormat.format(mockBean.date),
                mockBean.localDateTime.format(localDateTimeFormat)),
                mapper.writeValueAsString(mockBean));
    }

    @Data
    private static class MockBean {
        private Map<Integer, String> prop;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Date date;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private LocalDateTime localDateTime;
    }

    @Test
    public void testCompositePrincipal() throws Exception {
        UserCompositePrincipal.Builder builder = UserCompositePrincipal.builder();
        UserCompositePrincipal Bob = builder.username("Bob").email("Bob@test.com").build();
        UserCompositePrincipal Bob2 = builder.username("Bob2").email("Bob2@test.com").build();
        assertEquals("Bob", Bob.getUsername());
        assertEquals("Bob@test.com", Bob.getEmail());
        assertNotEquals(Bob, Bob2);

        String res = mapper.writeValueAsString(Bob);
        assertEquals(Bob, mapper.readValue(res, UserCompositePrincipal.class));
    }

    @Test
    public void testExtendedUserDetailsImpl() throws Exception {
        ExtendedUserDetailsImpl expected = (ExtendedUserDetailsImpl) mockUserDetails();
        String res = mapper.writeValueAsString(expected);
        ExtendedUserDetailsImpl actual = mapper.readValue(res, ExtendedUserDetailsImpl.class);
        assertEquals(expected, actual);
    }

    @Test
    public void testAuthenticationImpl() throws Exception {
        AuthenticationImpl expected = (AuthenticationImpl) mockAuthFromDetails(mockUserDetails());
        String res = mapper.writeValueAsString(expected);
        AuthenticationImpl actual = mapper.readValue(res, AuthenticationImpl.class);
        assertEquals(expected, actual);
    }

    private UserDetails mockUserDetails() {
        final ExtendedUserDetailsImpl.Builder b = new ExtendedUserDetailsImpl.Builder();
        b.setUsername("one");
        b.setPassword("secret");
        b.setTitle("test username");
        b.setEmail("e@e.e");
        b.setEnabled(true);
        b.setAccountNonLocked(true);
        b.setAccountNonExpired(true);
        b.setCredentialsNonExpired(true);
        b.setTenant("34l");
        b.setAuthorities(Arrays.asList(
                new GrantedAuthorityImpl("ga1", "3l"),
                new GrantedAuthorityImpl("ga2", "6l")));
        return b.build();
    }

    private Authentication mockAuthFromDetails(UserDetails user) {
        return AuthenticationImpl.builder()
                .authorities(user.getAuthorities())
                .authenticated(true)
                .principal(user)
                .name(user.getUsername())
                .build();
    }
}

package at.fhv.eventmangement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class NavigationAcceptanceTest {

    // href of every item in the navigation bar
    private static final Pattern NAV_LINK = Pattern.compile("<a class=\"SiteNavLink\"\\s+href=\"([^\"]+)\"");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void everyNavigationItem_leadsToAnExistingPage() throws Exception {
        String home = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<String> targets = new ArrayList<>();
        Matcher matcher = NAV_LINK.matcher(home);
        while (matcher.find()) {
            targets.add(matcher.group(1));
        }
        assertThat(targets).containsExactly("/", "/customer/events", "/customer/bookings", "/events");

        for (String target : targets) {
            int status = mockMvc.perform(get(target)).andReturn().getResponse().getStatus();
            assertThat(status).as("navigation target %s", target).isLessThan(400);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"/", "/home", "/customer/events", "/events", "/event/manage/create"})
    void everyMainPage_showsNavigation(String page) throws Exception {
        mockMvc.perform(get(page))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("class=\"SiteNav\"")))
                .andExpect(content().string(containsString("class=\"SiteLogo\" href=\"/\"")));
    }
}
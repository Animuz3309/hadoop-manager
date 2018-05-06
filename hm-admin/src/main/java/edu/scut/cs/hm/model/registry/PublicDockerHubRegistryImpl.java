package edu.scut.cs.hm.model.registry;

import edu.scut.cs.hm.common.utils.Throwables;
import edu.scut.cs.hm.docker.model.image.ImageCatalog;
import edu.scut.cs.hm.docker.model.image.SearchResult;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.springframework.web.util.UriComponentsBuilder.newInstance;

@Slf4j
public class PublicDockerHubRegistryImpl extends AbstractV2RegistryService implements DockerHubRegistry {

    private final String dockerHubSearchRegistryUrl;

    @Builder
    public PublicDockerHubRegistryImpl(RegistryAdapter adapter,
                                       String dockerHubSearchRegistryUrl) {
        super(adapter);
        this.dockerHubSearchRegistryUrl = dockerHubSearchRegistryUrl;
    }

    @Override
    public SearchResult search(String searchTerm, int page, int count) {
        try {
            // GET /v1/search?q=search_term&page=1&n=25 HTTP/1.1
            UriComponents build = getBasePath().pathSegment("search")
                    .queryParam("q", searchTerm)
                    .queryParam("page", page + 1 /* hub numbers pages from 1 instead of 0*/)
                    .queryParam("n", count)
                    .build().encode(StandardCharsets.UTF_8);
            SearchResult res = getRestTemplate().getForObject(build.toUri(), SearchResult.class);
            //first page in hub will start from '1', it may confuse our api users
            res.setPage(res.getPage() - 1);
            res.getResults().forEach(r -> r.getRegistries().add(getConfig().getName()));
            return res;
        } catch (HttpStatusCodeException e) {
            // error logged internal
            processStatusCodeException(e);
            return null;
        } catch (Exception e) {
            throw Throwables.asRuntime(e);
        }
    }

    private UriComponentsBuilder getBasePath() throws URISyntaxException {
        return newInstance().uri(new URI(dockerHubSearchRegistryUrl)).path("v1");
    }

    @Override
    public ImageCatalog getCatalog() {
        return new ImageCatalog(Collections.emptyList());
    }
}

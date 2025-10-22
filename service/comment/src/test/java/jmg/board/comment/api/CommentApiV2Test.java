package jmg.board.comment.api;

import jmg.board.comment.service.response.CommentPageResponse;
import jmg.board.comment.service.response.CommentResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

public class CommentApiV2Test {
    RestClient restClient = RestClient.create("http://localhost:9001");

    @Test
    void create() {
        CommentResponse response1 = create(new CommentCreateRequestV2(1L, "my comment1", null, 1L));
        CommentResponse response2 = create(new CommentCreateRequestV2(1L, "my comment2", response1.getPath(), 1L));
        CommentResponse response3 = create(new CommentCreateRequestV2(1L, "my comment3", response2.getPath(), 1L));

        System.out.println("response1.getPath() = " + response1.getPath());
        System.out.println("response1.getCommentId() = " + response1.getCommentId());
        System.out.println("\tresponse2.getPath() = " + response2.getPath());
        System.out.println("\tresponse2.getCommentId() = " + response2.getCommentId());
        System.out.println("\t\tresponse3.getPath() = " + response3.getPath());
        System.out.println("\t\tresponse3.getCommentId() = " + response3.getCommentId());
    }

    CommentResponse create(CommentCreateRequestV2 request) {
        return restClient.post()
                .uri("/v2/comments")
                .body(request)
                .retrieve()
                .body(CommentResponse.class);
    }

    /**
     * response1.getPath() = 00002
     * response1.getCommentId() = 238673396016238592
     * 	response2.getPath() = 0000200000
     * 	response2.getCommentId() = 238673397467467776
     * 		response3.getPath() = 000020000000000
     * 		response3.getCommentId() = 238673397719126016
     */

    @Test
    void read() {
        CommentResponse response = restClient.get()
                .uri("/v2/comments/{commentId}", 238673396016238592L)
                .retrieve()
                .body(CommentResponse.class);

        System.out.println("response = " + response);
    }

    @Test
    void delete() {
        restClient.delete()
                .uri("/v2/comments/{commentId}", 238673396016238592L)
                .retrieve();
    }
    
    @Test
    void readAll() {
        CommentPageResponse response = restClient.get()
                .uri("/v2/comments?articleId=1&pageSize=10&page=50000")
                .retrieve()
                .body(CommentPageResponse.class);

        System.out.println("response.getCommentCount() = " + response.getCommentCount());

        for(CommentResponse comment : response.getComments()) {
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }
    }

    /**
     * comment.getCommentId() = 238673008160559104
     * comment.getCommentId() = 238673013759954944
     * comment.getCommentId() = 238673014124859392
     * comment.getCommentId() = 238673161793720320
     * comment.getCommentId() = 238673164679401472
     * comment.getCommentId() = 238673166206128128
     * comment.getCommentId() = 238673396016238592
     * comment.getCommentId() = 238673397467467776
     * comment.getCommentId() = 238673397719126016
     * comment.getCommentId() = 238675323774496768
     */

    @Test
    void readAllInfiniteScroll() {
        List<CommentResponse> responses1 = restClient.get()
                .uri("/v2/comments/infinite-scroll?articleId=1&pageSize=5")
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponse>>() {
                });

        System.out.println("fisrtPage");
        for(CommentResponse comment : responses1) {
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }

        System.out.println("secondPage");
        String lastPath = responses1.getLast().getPath();

        List<CommentResponse> responses2 = restClient.get()
                .uri("/v2/comments/infinite-scroll?articleId=1&pageSize=5&lastPath=%s".formatted(lastPath))
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponse>>() {
                });

        for(CommentResponse comment : responses2) {
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }

    }

    @Getter
    @AllArgsConstructor
    public static class CommentCreateRequestV2 {
        private Long articleId;
        private String content;
        private String parentPath;
        private Long writerId;
    }
}

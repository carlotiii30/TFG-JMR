package jmr.descriptor.generated;

import jmr.descriptor.Comparator;
import jmr.descriptor.color.SingleColorDescriptor;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import javax.imageio.ImageIO;

/**
 * Descriptor that generates an image from a text prompt using an external API
 * (Hugging Face) and computes descriptors based on the resulting image.
 *
 * @author Carlota de la Vega
 */
public class PromptGeneratedImageDescriptor
        extends AbstractPromptImageDescriptor<PromptGeneratedImageDescriptor> {

    private static final String DEFAULT_API_TOKEN = "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";
    private static final String API_URL = "https://api-inference.huggingface.co/models/stabilityai/stable-diffusion-xl-base-1.0";

    private final String apiToken;

    /**
     * Creates a new remote prompt-based image descriptor using the default API
     * token.
     *
     * @param prompt The textual prompt used for image generation.
     */
    public PromptGeneratedImageDescriptor(String prompt) {
        this(prompt, DEFAULT_API_TOKEN);
    }

    /**
     * Creates a new remote prompt-based image descriptor using a custom API
     * token.
     *
     * @param prompt The textual prompt used for image generation.
     * @param apiToken The Hugging Face API token to use.
     */
    public PromptGeneratedImageDescriptor(String prompt, String apiToken) {
        super(prompt, new DefaultComparator());
        this.apiToken = apiToken;
    }

    @Override
    public void init(String prompt) {
        super.init(prompt);
        if (generatedImage != null) {
            descriptorList.add(new SingleColorDescriptor(generatedImage));
        }
    }

    /**
     * Sends the prompt to Hugging Face API and returns the generated image.
     *
     * @param prompt The textual prompt.
     * @return The {@link BufferedImage} result, or {@code null} on error.
     */
    @Override
    protected BufferedImage generateImage(String prompt) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .build();

            String jsonInputString = String.format("{\"inputs\": \"%s\"}", prompt);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + apiToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonInputString))
                    .build();

            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() == 200) {
                try (InputStream inputStream = response.body()) {
                    return ImageIO.read(inputStream);
                }
            } else {
                System.err.println("API error: " + response.statusCode());
                return null;
            }

        } catch (IOException | InterruptedException e) {
            return null;
        }
    }

    /**
     * Comparator for comparing two remote descriptors based on their visual
     * content.
     */
    public static class DefaultComparator implements Comparator<PromptGeneratedImageDescriptor, Double> {

        @Override
        public Double apply(PromptGeneratedImageDescriptor a, PromptGeneratedImageDescriptor b) {
            if (a.getDescriptorList() != null && b.getDescriptorList() != null) {
                return a.getDescriptorList().compare(b.getDescriptorList());
            } else {
                return Double.MAX_VALUE;
            }
        }
    }
}

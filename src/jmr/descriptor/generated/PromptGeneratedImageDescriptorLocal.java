package jmr.descriptor.generated;

import jmr.descriptor.Comparator;
import jmr.descriptor.color.SingleColorDescriptor;
import org.json.JSONObject;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import javax.imageio.ImageIO;

/**
 * Descriptor that generates an image from a text prompt using a local API,
 * and computes visual descriptors for that image.
 * 
 * @author Carlota de la Vega
 */
public class PromptGeneratedImageDescriptorLocal
        extends AbstractPromptImageDescriptor<PromptGeneratedImageDescriptorLocal> {

    private static final String BASE_URL = "http://localhost:8000";

    /**
     * Creates a new local prompt-based image descriptor.
     *
     * @param prompt The textual prompt used for image generation.
     */
    public PromptGeneratedImageDescriptorLocal(String prompt) {
        super(prompt, new DefaultComparator());
    }

    @Override
    public void init(String prompt) {
        super.init(prompt);
        if (generatedImage != null) {
            descriptorList.add(new SingleColorDescriptor(generatedImage));
        }
    }

    /**
     * Sends the prompt to a local API to generate the corresponding image.
     *
     * @param prompt The text prompt.
     * @return The generated {@link BufferedImage}, or {@code null} on error.
     */
    @Override
    protected BufferedImage generateImage(String prompt) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .build();

            String jsonInputString = String.format("""
                {
                    "model_name": "stable",
                    "prompt": "%s",
                    "num_inference_steps": 50,
                    "guidance_scale": 7.5
                }
            """, prompt.replace("\"", "\\\""));

            HttpRequest postRequest = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/images/generate/"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonInputString))
                    .build();

            HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());

            if (postResponse.statusCode() != 200) {
                System.err.println("❌ Error generating image: " + postResponse.statusCode());
                return null;
            }

            JSONObject json = new JSONObject(postResponse.body());
            String imagePath = json.getString("image_path");
            String imageName = imagePath.substring(imagePath.lastIndexOf("/") + 1);

            HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/images/download/" + imageName))
                    .GET()
                    .build();

            HttpResponse<InputStream> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofInputStream());

            if (getResponse.statusCode() != 200) {
                System.err.println("❌ Error downloading image: " + getResponse.statusCode());
                return null;
            }

            return ImageIO.read(getResponse.body());

        } catch (IOException | InterruptedException e) {
            return null;
        }
    }

    /**
     * Comparator for comparing two local descriptors based on their visual content.
     */
    public static class DefaultComparator implements Comparator<PromptGeneratedImageDescriptorLocal, Double> {
        @Override
        public Double apply(PromptGeneratedImageDescriptorLocal a, PromptGeneratedImageDescriptorLocal b) {
            if (a.getDescriptorList() != null && b.getDescriptorList() != null) {
                return a.getDescriptorList().compare(b.getDescriptorList());
            } else {
                return Double.MAX_VALUE;
            }
        }
    }
}

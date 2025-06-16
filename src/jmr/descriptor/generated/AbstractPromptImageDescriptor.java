package jmr.descriptor.generated;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import jmr.descriptor.Comparator;
import jmr.descriptor.DescriptorList;
import jmr.descriptor.MediaDescriptorAdapter;

/**
 * Abstract base class for image descriptors generated from text prompts.
 * <p>
 * This class provides common functionality for descriptors that generate an
 * image from a text prompt (e.g., via an external or local API) and then
 * compute a list of visual descriptors based on that image.
 * </p>
 *
 * @param <T> The concrete subclass type used for comparator typing.
 * 
 * @author Carlota de la Vega
 */
public abstract class AbstractPromptImageDescriptor<T extends AbstractPromptImageDescriptor<T>>
        extends MediaDescriptorAdapter<String> implements Serializable {

    /**
     * The image generated from the input prompt.
     */
    protected BufferedImage generatedImage;

    /**
     * A list of visual descriptors extracted from the generated image.
     */
    protected DescriptorList<BufferedImage> descriptorList;

    /**
     * Constructs the descriptor using the given prompt and comparator.
     *
     * @param prompt     The textual prompt to generate the image.
     * @param comparator A comparator for comparing two descriptors of this type.
     */
    public AbstractPromptImageDescriptor(String prompt, Comparator<T, Double> comparator) {
        super(prompt, comparator);
    }

    /**
     * Initializes the descriptor: generates the image using the given prompt and
     * constructs the descriptor list from the resulting image.
     *
     * @param prompt The textual prompt used to generate the image.
     */
    @Override
    public void init(String prompt) {
        this.generatedImage = generateImage(prompt);
        if (generatedImage != null) {
            descriptorList = new DescriptorList<>(generatedImage);
        }
    }

    /**
     * Returns the image generated from the prompt.
     *
     * @return The generated {@link BufferedImage}, or {@code null} if generation failed.
     */
    public BufferedImage getGeneratedImage() {
        return generatedImage;
    }

    /**
     * Returns the list of visual descriptors extracted from the generated image.
     *
     * @return A {@link DescriptorList} containing descriptors for the image.
     */
    public DescriptorList<BufferedImage> getDescriptorList() {
        return descriptorList;
    }

    /**
     * Returns a string representation of the descriptor, including the prompt and
     * descriptor list.
     *
     * @return A human-readable description of this descriptor.
     */
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ": [" + source + "]\n" +
                (descriptorList != null ? descriptorList.toString() : "No descriptors generated.");
    }

    /**
     * Generates an image based on the given textual prompt.
     * Subclasses must implement this method to define the actual generation process.
     *
     * @param prompt The prompt to be used for image generation.
     * @return The generated {@link BufferedImage}, or {@code null} on failure.
     */
    protected abstract BufferedImage generateImage(String prompt);
}

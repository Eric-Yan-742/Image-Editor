/**
 * Name: Eric Yan
 * E-mail: yuy061@ucsd.edu
 * PID: A17341154
 * Reference: write-up
 * ImageEditor can rotate an image, lower the resolution of an image, and 
 * patch a samller image onto an image.
 */
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
/**
 * Class ImageEditor can rotate an image, lower the resolution of an image,
 * and patch a smaller image onto the image.
 */
public class ImageEditor {
    /* Constants (Magic numbers) */
    private static final String PNG_FORMAT = "png";
    private static final String NON_RGB_WARNING =
            "Warning: we do not support the image you provided. \n" +
            "Please change another image and try again.";
    private static final String RGB_TEMPLATE = "(%3d, %3d, %3d) ";
    private static final int BLUE_BYTE_SHIFT = 0;
    private static final int GREEN_BYTE_SHIFT = 8;
    private static final int RED_BYTE_SHIFT = 16;
    private static final int ALPHA_BYTE_SHIFT = 24;
    private static final int BLUE_BYTE_MASK = 0xff << BLUE_BYTE_SHIFT;
    private static final int GREEN_BYTE_MASK = 0xff << GREEN_BYTE_SHIFT;
    private static final int RED_BYTE_MASK = 0xff << RED_BYTE_SHIFT;
    private static final int ALPHA_BYTE_MASK = ~(0xff << ALPHA_BYTE_SHIFT);

    /*Eric Yan's magic numbers */
    private static final int RED_DIVISOR = (int)Math.pow(16, 4);
    private static final int GREEN_DIVISOR = (int)Math.pow(16, 2);
    private static final int NINETY_DEGREE = 90;
    private static final int THREE_HUNDRED_SIXTY_DEGREE = 360;

    /* Static variables - DO NOT add any additional static variables */
    static int[][] image;

    /**
     * Open an image from disk and return a 2D array of its pixels.
     * Use 'load' if you need to load the image into 'image' 2D array instead
     * of returning the array.
     *
     * @param pathname path and name to the file, e.g. "input.png",
     *                 "D:\\Folder\\ucsd.png" (for Windows), or
     *                 "/User/username/Desktop/my_photo.png" 
     *                  (for Linux/macOS).
     *                 Do NOT use "~/Desktop/xxx.png" (not supported in Java).
     * @return 2D array storing the rgb value of each pixel in the image
     * @throws IOException when file cannot be found or read
     */
    public static int[][] open(String pathname) throws IOException {
        BufferedImage data = ImageIO.read(new File(pathname));
        if (data.getType() != BufferedImage.TYPE_3BYTE_BGR &&
                data.getType() != BufferedImage.TYPE_4BYTE_ABGR) {
            System.err.println(NON_RGB_WARNING);
        }
        int[][] array = new int[data.getHeight()][data.getWidth()];

        for (int row = 0; row < data.getHeight(); row++) {
            for (int column = 0; column < data.getWidth(); column++) {
                /* Images are stored by column major
                   i.e. (2, 10) is the pixel on the column 2 and row 10
                   However, in class, arrays are in row major
                   i.e. [2][10] is the 11th element on the 2nd row
                   So we reverse the order of i and j when we load the image.
                 */
                array[row][column] = data.getRGB(column, row) 
                        & ALPHA_BYTE_MASK;
            }
        }

        return array;
    }

    /**
     * Load an image from disk to the 'image' 2D array.
     *
     * @param pathname path and name to the file, see open for examples.
     * @throws IOException when file cannot be found or read
     */ 
    public static void load(String pathname) throws IOException {
        image = open(pathname);
    }

    /**
     * Save the 2D image array to a PNG file on the disk.
     *
     * @param pathname path and name for the file. Should be different from
     *                 the input file. See load for examples.
     * @throws IOException when file cannot be found or written
     */
    public static void save(String pathname) throws IOException {
        BufferedImage data = new BufferedImage(
                image[0].length, image.length, BufferedImage.TYPE_INT_RGB);
        for (int row = 0; row < data.getHeight(); row++) {
            for (int column = 0; column < data.getWidth(); column++) {
                // reverse it back when we write the image
                data.setRGB(column, row, image[row][column]);
            }
        }
        ImageIO.write(data, PNG_FORMAT, new File(pathname));
    }

    /**
     * Unpack red byte from a packed RGB int
     *
     * @param rgb RGB packed int
     * @return red value in that packed pixel, 0 <= red <= 255
     */
    private static int unpackRedByte(int rgb) {
        return (rgb & RED_BYTE_MASK) >> RED_BYTE_SHIFT;
    }

    /**
     * Unpack green byte from a packed RGB int
     *
     * @param rgb RGB packed int
     * @return green value in that packed pixel, 0 <= green <= 255
     */
    private static int unpackGreenByte(int rgb) {
        return (rgb & GREEN_BYTE_MASK) >> GREEN_BYTE_SHIFT;
    }

    /**
     * Unpack blue byte from a packed RGB int
     *
     * @param rgb RGB packed int
     * @return blue value in that packed pixel, 0 <= blue <= 255
     */
    private static int unpackBlueByte(int rgb) {
        return (rgb & BLUE_BYTE_MASK) >> BLUE_BYTE_SHIFT;
    }

    /**
     * Pack RGB bytes back to an int in the format of
     * [byte0: unused][byte1: red][byte2: green][byte3: blue]
     *
     * @param red   red byte, must satisfy 0 <= red <= 255
     * @param green green byte, must satisfy 0 <= green <= 255
     * @param blue  blue byte, must satisfy 0 <= blue <= 255
     * @return packed int to represent a pixel
     */
    private static int packInt(int red, int green, int blue) {
        return (red << RED_BYTE_SHIFT)
                + (green << GREEN_BYTE_SHIFT)
                + (blue << BLUE_BYTE_SHIFT);
    }

    /**
     * Print the current image 2D array in (red, green, blue) format.
     * Each line represents a row in the image.
     */
    public static void printImage() {
        for (int[] ints : image) {
            for (int pixel : ints) {
                System.out.printf(
                        RGB_TEMPLATE,
                        unpackRedByte(pixel),
                        unpackGreenByte(pixel),
                        unpackBlueByte(pixel));
            }
            System.out.println();
        }
    }
    /**
     * Rotate an image with a given degree
     * @param degree the given degree of rotation. Must be greater than zero
     * and divisible by 90
     */
    public static void rotate(int degree) {
        //Check Invalid Output
        if(degree < 0 || degree % NINETY_DEGREE != 0) {
            return;
        }
        degree = degree % THREE_HUNDRED_SIXTY_DEGREE;
        if(degree == 0) {
            return;
        }
        //Back up the original image
        int[][] old_image = new int[image.length][image[0].length];
        for(int i = 0; i < image.length; ++i) {
            for(int j = 0; j < image[i].length; ++j) {
                old_image[i][j] = image[i][j];
            }
        }
        //Get the new image
        int number_of_row_in_old = image.length;
        image = new int[image[0].length][image.length];
        for(int i = 0; i < old_image.length; ++i) {
            for(int j = 0; j < old_image[0].length; ++j) {
                image[j][number_of_row_in_old - 1 - i] = old_image[i][j];
            }
        }
        if(degree > NINETY_DEGREE) {
            rotate(degree - NINETY_DEGREE);
        }
    }
    /**
     * Lower the resolution of an image
     * @param heightScale down-scaling factor for the height of the image.
     * Must be greater or equal to 1 and smaller than the height of the 
     * image 
     * @param widthScale down-scaling factor for the width of the image.
     * Must be greater or equal to 1 and smaller than the width of the
     * image
     */
    public static void downSample(int heightScale, int widthScale) {
        //Check invalid scale
        if(heightScale < 1 || widthScale < 1 || heightScale > image.length 
                || widthScale > image[0].length 
                || image.length % heightScale != 0 
                || image[0].length % widthScale != 0) {
            return;
        }
        int[][] scaledImage = new int[image.length / heightScale]
                [image[0].length / widthScale];
        for(int scaledRow = 0, newRow = 0; scaledRow < image.length; 
                scaledRow += heightScale, ++newRow) {
            for(int scaledColumn = 0, newColomn = 0; 
                    scaledColumn < image[scaledRow].length; scaledColumn 
                    += widthScale, ++newColomn) {
                //Calculate the average rgb of each scaled pixel
                int red = 0, green = 0, blue = 0;
                for(int i = scaledRow; i < scaledRow + heightScale; ++i) {
                    for(int j = scaledColumn; j < scaledColumn + widthScale; 
                            ++j) {
                        int rgb = image[i][j];
                        red += rgb/RED_DIVISOR;
                        rgb %= RED_DIVISOR;
                        green += rgb/GREEN_DIVISOR;
                        rgb %= GREEN_DIVISOR;
                        blue += rgb;
                    }
                }
                red /= heightScale * widthScale;
                green /= heightScale * widthScale;
                blue /= heightScale * widthScale;
                int newRgb = red * RED_DIVISOR + green * (int)GREEN_DIVISOR 
                        + blue;
                //Paint new image with average rgb
                scaledImage[newRow][newColomn] = newRgb;
            }
        }
        image = scaledImage;
    }
    /**
     * Patch a smaller image onto the image at the given position. If patch
     * image's pixel's rgb matches the transparent rgb, do not replace the
     * pixel of image with the pixel of the patch image
     * @param startRow the starting row position of the patch image. Must be
     * greater or equal to 0 and smaller than the width of the big image.
     * @param startColumn the starting column position of the patch image.
     * Must be greater or equal to 0 and smaller than the width of the big
     * image.
     * @param patchImage a 2D array containing the RGB of the patch image.
     * If the image is not within the big image, patch is invalid
     * @param transparentRed red rgb of the transparent color
     * @param transparentGreen green rgb of the transparent color
     * @param transparentBlue blue rgb of the transparent color
     * @return the number of pixels that are replaced
     */
    public static int patch(int startRow, int startColumn, 
            int[][] patchImage, int transparentRed, int transparentGreen, 
            int transparentBlue) {
        int pHeight = patchImage.length, pWidth = patchImage[0].length;
        //Check invalid starting position
        if(startRow < 0 || startColumn < 0 || startRow > image.length 
                || startColumn > image[0].length) {
            return 0;
        }
        //Check the patch image is within the image
        if(startRow + pHeight > image.length 
                || startColumn + pWidth > image[0].length) {
            return 0;
        }
        
        int count = 0;
        for(int pr = 0; pr < pHeight; ++pr) {
            for(int pc = 0; pc < pWidth; ++pc) {
                //Get the rgb of patch image's pixel
                int pRgb = patchImage[pr][pc];
                int pRed = pRgb / RED_DIVISOR;
                pRgb %= RED_DIVISOR;
                int pGreen = pRgb / GREEN_DIVISOR;
                pRgb %= GREEN_DIVISOR;
                int pBlue = pRgb;
                //Check whether pixel rgb matches transparent rgb
                if(pRed == transparentRed && pGreen == transparentGreen 
                        && pBlue == transparentBlue) {
                    continue;
                }
                image[pr+startRow][pc+startColumn] = patchImage[pr][pc];
                ++count;
            }
        }
        return count;
    }
}
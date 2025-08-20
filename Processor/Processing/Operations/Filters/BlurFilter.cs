using SixLabors.ImageSharp;
using SixLabors.ImageSharp.PixelFormats;
using Processing.Interfaces;

public class BlurFilter : IImageOperation
{
    public void Apply(Image<Rgba32> image, Dictionary<string, string> parameters)
    {
        // creating gaussian kernel
        int blurRadius = 7;
        float sigma = blurRadius / 3f;

        int size = blurRadius * 2 + 1;
        float[,] kernel = new float[size, size];

        float twoSigmaSq = 2 * sigma * sigma;
        float piSigma =  (float)(Math.PI * twoSigmaSq);
        float kernelSum = 0;

        for (int y = 0; y < size; y++)
        {
            for (int x = 0; x < size; x++)
            {
                int dy = y - blurRadius;
                int dx = x - blurRadius;
                float value = (float)Math.Exp(-(dx * dx + dy * dy) / twoSigmaSq) / piSigma;

                kernel[y, x] = value;
                kernelSum += value;
            }
        }

        // normalizing
        for (int y = 0; y < size; y++)
        {
            for (int x = 0; x < size; x++)
            {
                kernel[y, x] /= kernelSum;
            }
        }

        int kernelSize = kernel.GetLength(0);
        int offset = dia / 2;

        using (var source = image.Clone())
        {
            for (int y = offset; y < image.Height - offset; y++)
            {
                for (int x = offset; x < image.Width - offset; x++)
                {
                    float r = 0, g = 0, b = 0, a = 0;
                    float weightSum = 0;

                    for (int ky = 0; ky < kernelSize; ky++)
                    {
                        for (int kx = 0; kx < kernelSize; kx++)
                        {
                            int px = x + kx - 1;
                            int py = y + ky - 1;

                            Rgba32 pixel = source[px, py];
                            float weight = kernel[ky, kx];

                            r += pixel.R * weight;
                            g += pixel.G * weight;
                            b += pixel.B * weight;
                            a += pixel.A * weight;

                            weightSum += weight;
                        }
                    }

                    r /= kernelSum;
                    g /= kernelSum;
                    b /= kernelSum;
                    a /= kernelSum;

                    image[x, y] = new Rgba32((byte)r, (byte)g, (byte)b, (byte)a);
                }
            }
        }
    }
}
using SixLabors.ImageSharp;
using SixLabors.ImageSharp.PixelFormats;
using Processing.Interfaces;

public class BlurFilter : IImageOperation
{
    public const int DefaultRadius = 3;
    public void Apply(Image<Rgba32> image, Dictionary<string, string> parameters)
    {
        int blurRadius = DefaultRadius;

        using (var source = image.Clone())
        {
            int kernelSize = blurRadius;
            int[,] kernel = new int[kernelSize, kernelSize];
            int kernelSum = 0;

            for (int i = 0; i < kernelSize; i++)
            {
                for (int j = 0; j < kernelSize; j++)
                {
                    kernel[i, j] = 1;
                    kernelSum++;
                }
            }

            int offset = kernelSize / 2;

            for (int y = offset; y < image.Height - offset; y++)
            {
                for (int x = offset; x < image.Width - offset; x++)
                {
                    int r = 0, g = 0, b = 0, a = 0;
                    for (int ky = 0; ky < kernelSize; ky++)
                    {
                        for (int kx = 0; kx < kernelSize; kx++)
                        {
                            int px = x + kx - 1;
                            int py = y + ky - 1;

                            Rgba32 pixel = source[px, py];
                            int kernelValue = kernel[ky, kx];

                            r += pixel.R * kernelValue;
                            g += pixel.G * kernelValue;
                            b += pixel.B * kernelValue;
                            a += pixel.A * kernelValue;
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
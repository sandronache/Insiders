using SixLabors.ImageSharp;
using SixLabors.ImageSharp.PixelFormats;
using Processing.Interfaces;

namespace Processing.Operations;

public class SharpenFilter : IImageOperation
{
    /* Parameters: none. */
    public void Apply(Image<Rgba32> image, Dictionary<string, string> parameters)
    {
        /* Kernel 3x3. */
        float[,] kernel = {
            { 0, -1,  0 },
            {-1,  5, -1 },
            { 0, -1,  0 }
        };
        ApplyConvolution(image, kernel);
    }

    private void ApplyConvolution(Image<Rgba32> image, float[,] kernel)
    {
        var copy = image.Clone();

        image.ProcessPixelRows(accessor =>
        {
            for (int y = 1; y < accessor.Height - 1; y++)
            {
                Span<Rgba32> pixelRow = accessor.GetRowSpan(y);

                for (int x = 1; x < pixelRow.Length - 1; x++)
                {
                    float r = 0, g = 0, b = 0;

                    for (int ky = -1; ky <= 1; ky++)
                    {
                        for (int kx = -1; kx <= 1; kx++)
                        {
                            var pixel = copy[x + kx, y + ky];
                            float weight = kernel[ky + 1, kx + 1];
                            r += pixel.R * weight;
                            g += pixel.G * weight;
                            b += pixel.B * weight;
                        }
                    }

                    ref Rgba32 targetPixel = ref pixelRow[x];
                    targetPixel.R = (byte)Math.Clamp(r, 0, 255);
                    targetPixel.G = (byte)Math.Clamp(g, 0, 255);
                    targetPixel.B = (byte)Math.Clamp(b, 0, 255);
                }
            }
        });
    }
}
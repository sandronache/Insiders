using SixLabors.ImageSharp;
using SixLabors.ImageSharp.PixelFormats;
using Processing.Interfaces;

namespace Processing.Operations;

public class NoiseFilter : IImageOperation
{
    /* Parameters: intensity (float, default: 25), range: 0-100) */
    public void Apply(Image<Rgba32> image, Dictionary<string, string> parameters)
    {
        int intensity = 25;
        if (parameters.ContainsKey("Intensity") && int.TryParse(parameters["Intensity"], out int parsedIntensity))
        {
            intensity = Math.Clamp(parsedIntensity, 0, 100);
        }

        Random random = new Random();

        image.ProcessPixelRows(accessor =>
        {
            for (int y = 0; y < accessor.Height; y++)
            {
                Span<Rgba32> pixelRow = accessor.GetRowSpan(y);

                for (int x = 0; x < pixelRow.Length; x++)
                {
                    ref Rgba32 pixel = ref pixelRow[x];

                    int noiseR = random.Next(-intensity, intensity + 1);
                    int noiseG = random.Next(-intensity, intensity + 1);
                    int noiseB = random.Next(-intensity, intensity + 1);

                    pixel.R = (byte)Math.Clamp(pixel.R + noiseR, 0, 255);
                    pixel.G = (byte)Math.Clamp(pixel.G + noiseG, 0, 255);
                    pixel.B = (byte)Math.Clamp(pixel.B + noiseB, 0, 255);
                }
            }
        });
    }
}

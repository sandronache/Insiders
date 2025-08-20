using SixLabors.ImageSharp;
using SixLabors.ImageSharp.PixelFormats;
using Processing.Interfaces;

namespace Processing.Operations;

public class TintFilter : IImageOperation
{
    // small blue tint
    public const byte rTint = 0;
    public const byte gTint = 50;
    public const byte bTint = 150;

    public void Apply(Image<Rgba32> image, Dictionary<string, string> parameters)
    {
        for (int y = 0; y < image.Height; y++)
        {
            for (int x = 0; x < image.Width; x++)
            {
                Rgba32 pixel = image[x, y];

                int r = Math.Clamp(pixel.R + rTint, 0, 255);
                int g = Math.Clamp(pixel.G + gTint, 0, 255);
                int b = Math.Clamp(pixel.B + bTint, 0, 255);

                image[x, y] = new Rgba32((byte)r, (byte)g, (byte)b, pixel.A);
            }
        }
    }
}

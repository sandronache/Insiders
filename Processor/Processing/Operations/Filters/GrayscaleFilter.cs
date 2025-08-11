using SixLabors.ImageSharp;
using SixLabors.ImageSharp.Processing;
using SixLabors.ImageSharp.PixelFormats;
using Processing.Interfaces;

namespace Processing.Operations;

public class GrayscaleFilter : IImageOperation
{
    /* Parameters: none. */
    public void Apply(Image<Rgba32> image, Dictionary<string, string> parameters)
    {
        image.ProcessPixelRows(accessor =>
        {
            for (int y = 0; y < accessor.Height; y++)
            {
                Span<Rgba32> pixelRow = accessor.GetRowSpan(y);
                
                for (int x = 0; x < pixelRow.Length; x++)
                {
                    ref Rgba32 pixel = ref pixelRow[x];
                    
                    /* Luminance: 0.299 * R + 0.587 * G + 0.114 * B. */
                    float gray = 0.299f * pixel.R + 0.587f * pixel.G + 0.114f * pixel.B;
                    byte grayByte = (byte)Math.Round(gray);
                    
                    pixel.R = grayByte;
                    pixel.G = grayByte;
                    pixel.B = grayByte;
                }
            }
        });
    }
}

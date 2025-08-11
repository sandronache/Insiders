using SixLabors.ImageSharp;
using SixLabors.ImageSharp.Processing;
using SixLabors.ImageSharp.PixelFormats;
using Processing.Interfaces;

namespace Processing.Operations;

public class SepiaFilter : IImageOperation
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
                   
                   /* Sepia standard formula. */
                   int newR = (int)(0.393 * pixel.R + 0.769 * pixel.G + 0.189 * pixel.B);
                   int newG = (int)(0.349 * pixel.R + 0.686 * pixel.G + 0.168 * pixel.B);
                   int newB = (int)(0.272 * pixel.R + 0.534 * pixel.G + 0.131 * pixel.B);
                   
                   pixel.R = (byte)Math.Min(255, newR);
                   pixel.G = (byte)Math.Min(255, newG);
                   pixel.B = (byte)Math.Min(255, newB);
               }
           }
       });
   }
}
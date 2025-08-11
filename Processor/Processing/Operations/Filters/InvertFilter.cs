using SixLabors.ImageSharp;
using SixLabors.ImageSharp.PixelFormats;
using SixLabors.ImageSharp.PixelFormats;
using Processing.Interfaces;

namespace Processing.Operations;

public class InvertFilter : IImageOperation
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
                   
                   pixel.R = (byte)(255 - pixel.R);
                   pixel.G = (byte)(255 - pixel.G);
                   pixel.B = (byte)(255 - pixel.B);
               }
           }
       });
   }
}


using Processing.Models;
using SixLabors.ImageSharp.Processing;

public class GrayscaleFilter : IImageOperation
{
    public void Apply(ImagePackage imagePackage, Dictionary<string, string> parameters)
    {
        imagePackage.Image.Mutate(ctx => ctx.Grayscale());
    }
}

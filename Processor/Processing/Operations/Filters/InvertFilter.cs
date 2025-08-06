using Processing.Models;
using SixLabors.ImageSharp;
using SixLabors.ImageSharp.Processing;

public class InvertFilter : IImageOperation
{
    public void Apply(ImagePackage image, Dictionary<string, string> parameters)
    {
        image.Image.Mutate(x => x.Invert());
    }
}
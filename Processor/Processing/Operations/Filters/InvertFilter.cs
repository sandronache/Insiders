using SixLabors.ImageSharp;
using SixLabors.ImageSharp.Processing;

namespace Processing.Operations;

public class InvertFilter : IImageOperation
{
    public void Apply(Image image, Dictionary<string, string> parameters)
    {
        image.Mutate(x => x.Invert());
    }
}
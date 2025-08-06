
using SixLabors.ImageSharp;
using SixLabors.ImageSharp.Processing;

public class GrayscaleFilter : IImageOperation
{
    public void Apply(Image image, Dictionary<string, string> parameters)
    {
        image.Mutate(x => x.Grayscale());
    }
}

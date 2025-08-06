using Processing.Models;
using SixLabors.ImageSharp;
using SixLabors.ImageSharp.Processing;

public class Resize : IImageOperation
{
    public void Apply(Image image, Dictionary<string, string> parameters)
    {
        int width = int.Parse(parameters["width"]);
        int height = int.Parse(parameters["height"]);
        image.Mutate(x => x.Resize(width, height));
    }
}

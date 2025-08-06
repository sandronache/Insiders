using Processing.Models;
using SixLabors.ImageSharp;

public interface IImageOperation
{
    public void Apply(Image image, Dictionary<string, string> parameters);
}
using SixLabors.ImageSharp;
using SixLabors.ImageSharp.PixelFormats;

namespace Processing.Interfaces;

public interface IImageOperation
{
    public void Apply(Image<Rgba32> image, Dictionary<string, string> parameters);
}
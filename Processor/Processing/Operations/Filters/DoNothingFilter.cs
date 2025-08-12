using SixLabors.ImageSharp;
using SixLabors.ImageSharp.PixelFormats;
using Processing.Interfaces;

namespace Processing.Operations;

public class DoNothingFilter : IImageOperation
{
    public void Apply(Image<Rgba32> image, Dictionary<string, string> parameters)
    {
        /* Doing nothing, I guess. */
    }
}
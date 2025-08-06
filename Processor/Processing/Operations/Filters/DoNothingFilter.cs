using SixLabors.ImageSharp;

namespace Processing.Operations;

public class DoNothingFilter : IImageOperation
{
    public void Apply(Image image, Dictionary<string, string> parameters)
    {
        /* Doing nothing, I guess. */
    }
}
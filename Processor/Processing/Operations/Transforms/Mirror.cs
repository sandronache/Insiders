using SixLabors.ImageSharp;
using SixLabors.ImageSharp.Processing;

namespace Processing.Operations;

public class Mirror : IImageOperation {

    public void Apply(Image image, Dictionary<string, string> parameters)
    {
        image.Mutate(x => x.Flip(FlipMode.Horizontal));
    }
}
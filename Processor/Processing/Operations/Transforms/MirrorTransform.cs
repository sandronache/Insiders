using SixLabors.ImageSharp;
using SixLabors.ImageSharp.Processing;
using SixLabors.ImageSharp.PixelFormats;
using Processing.Interfaces;

namespace Processing.Operations;

public class MirrorTransform : IImageOperation {

    public void Apply(Image<Rgba32> image, Dictionary<string, string> parameters)
    {
        image.Mutate(x => x.Flip(FlipMode.Horizontal));
    }
}
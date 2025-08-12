using Processing.Interfaces;
using SixLabors.ImageSharp;
using SixLabors.ImageSharp.PixelFormats;

namespace Processing.Validators;

public class FormatValidator : IImageValidator
{
    private const string ErrorMessage = "The image format is not supported.";

    private static readonly List<string> _formats = new List<string>
    {
        "JPG", "JPEG", "PNG", "BMP"
    };

    public string GetErrorMessage()
    {
        return ErrorMessage;
    }

    public bool Validate(Image image)
    {
        /* ToDo: Unknown ImageSharp format. */
        string format = image.Metadata.DecodedImageFormat.Name.ToUpperInvariant();
        return _formats.Contains(format);
    }
}

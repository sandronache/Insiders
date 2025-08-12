using Processing.Interfaces;
using SixLabors.ImageSharp;
using SixLabors.ImageSharp.PixelFormats;

namespace Processing.Validators;

public class SizeValidator : IImageValidator
{
    private const int MaxWidth = 2000;
    private const int MaxHeight = 2000;
    private const int MinWidth = 300;
    private const int MinHeight = 300;
    private const string ErrorMessage = "Image dimensions are outside the allowed range.";

    public string GetErrorMessage() {
        return ErrorMessage;
    }

    public bool Validate(Image image)
    {
        if (image.Width < MinWidth || image.Height < MinHeight ||
            image.Width > MaxWidth || image.Height > MaxHeight)
        {
            return false;
        }
        return true;
    }
}
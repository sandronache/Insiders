using System.IO;
using SixLabors.ImageSharp;
using SixLabors.ImageSharp.Formats;
using SixLabors.ImageSharp.PixelFormats;

namespace Processing.Models;

public class ImagePackage
{
    private readonly Image _image;
    private readonly IImageFormat _imageFormat;

    public Image Image => _image;
    public int Width => _image.Width;
    public int Height => _image.Height;
    public IImageFormat ImageFormat => _imageFormat;

    public ImagePackage(string imageBase64)
    {
        byte[] imageBytes = Convert.FromBase64String(imageBase64);
        using MemoryStream inputStream = new MemoryStream(imageBytes);

        _image = Image.Load(inputStream);
    }

    public void DisplayAttribute()
    {
        Console.WriteLine("Image: " + Image);
        Console.WriteLine("Format: " + ImageFormat);
    }
}


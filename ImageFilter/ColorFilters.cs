using System.Drawing;
using System.Drawing.Imaging;

namespace ImageFilter
{
    public class ColorFilters
    {
        // Color Inversion Filter
        public static Bitmap ApplyInvert(Bitmap source)
        {
            Bitmap result = new Bitmap(source.Width, source.Height);
            for (int y = 0; y < source.Height; y++)
            {
                for (int x = 0; x < source.Width; x++)
                {
                    Color pixel = source.GetPixel(x, y);
                    Color inverted = Color.FromArgb(
                        pixel.A,
                        255 - pixel.R,
                        255 - pixel.G,
                        255 - pixel.B
                    );
                    result.SetPixel(x, y, inverted);
                }
            }
            return result;
        }

        // Black and White (Grayscale) Filter
        public static Bitmap ApplyGrayscale(Bitmap source)
        {
            Bitmap result = new Bitmap(source.Width, source.Height);
            for (int y = 0; y < source.Height; y++)
            {
                for (int x = 0; x < source.Width; x++)
                {
                    Color pixel = source.GetPixel(x, y);
                    int gray = (int)(0.3 * pixel.R + 0.59 * pixel.G + 0.11 * pixel.B);
                    Color grayColor = Color.FromArgb(pixel.A, gray, gray, gray);
                    result.SetPixel(x, y, grayColor);
                }
            }
            return result;
        }

        // Blur Filter
        public static Bitmap ApplyBlur(Bitmap source)
        {
            Bitmap result = new Bitmap(source.Width, source.Height);

            int[,] kernel = {
                { 1, 1, 1 },
                { 1, 1, 1 },
                { 1, 1, 1 }
            };

            int kernelSize = 3;
            int kernelSum = 9;

            for (int y = 1; y < source.Height - 1; y++)
            {
                for (int x = 1; x < source.Width - 1; x++)
                {
                    int r = 0, g = 0, b = 0;

                    for (int ky = 0; ky < kernelSize; ky++)
                    {
                        for (int kx = 0; kx < kernelSize; kx++)
                        {
                            int px = x + kx - 1;
                            int py = y + ky - 1;

                            Color pixel = source.GetPixel(px, py);
                            int val = kernel[ky, kx];

                            r += pixel.R * val;
                            g += pixel.G * val;
                            b += pixel.B * val;
                        }
                    }

                    r /= kernelSum;
                    g /= kernelSum;
                    b /= kernelSum;

                    Color blurred = Color.FromArgb(255, r, g, b);
                    result.SetPixel(x, y, blurred);
                }
            }

            return result;
        }
    }
}

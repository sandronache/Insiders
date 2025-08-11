using Shared.Models;
using SixLabors.ImageSharp;
using SixLabors.ImageSharp.PixelFormats;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace Api.Utils;

public class Mapper
{

    public async Task<ProcessingRequest> ToRequestModel(ProcessingRequestDto request)
    {
        using var stream = new MemoryStream();
        await request.Image.CopyToAsync(stream);
        stream.Position = 0;
        Image image = Image.Load(stream);

        var options = new JsonSerializerOptions
        {
            Converters = { new JsonStringEnumConverter() }
        };
        List<OperationDescriptor> operations = JsonSerializer.Deserialize<List<OperationDescriptor>>(request.Operations, options);

        return new ProcessingRequest(image, operations);
    }
}

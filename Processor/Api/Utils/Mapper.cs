using SixLabors.ImageSharp;
using SixLabors.ImageSharp.PixelFormats;
using System.Text.Json;
using System.Text.Json.Serialization;
using Shared.Models;
using Api.Dtos;

namespace Api.Utils;

public class Mapper
{

    public async Task<ProcessingRequest> ToRequestModel(ProcessingRequestDto request)
    {
        using var stream = new MemoryStream();
        await request.Image.CopyToAsync(stream);
        stream.Position = 0;
        Image<Rgba32> image = Image.Load<Rgba32>(stream);

        var options = new JsonSerializerOptions
        {
            Converters = { new JsonStringEnumConverter() }
        };
        List<OperationDescriptor> operations = JsonSerializer.Deserialize<List<OperationDescriptor>>(request.Operations, options);

        return new ProcessingRequest(image, operations);
    }
}

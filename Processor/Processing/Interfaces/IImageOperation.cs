using Processing.Models;

public interface IImageOperation
{
    public void Apply(ImagePackage imagePacket, Dictionary<string, string> parameters);
}
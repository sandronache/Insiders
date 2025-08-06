namespace Processing.Operations;
public class ImageOperationFactory
{
    private readonly Dictionary<OperationType, IImageOperation> _operations;

    public ImageOperationFactory()
    {
        _operations = new Dictionary<OperationType, IImageOperation>()
        {
            { OperationType.Grayscale, new GrayscaleFilter() },
            { OperationType.DoNothing, new DoNothingFilter() },
            { OperationType.Mirror, new Mirror() },
            { OperationType.Resize, new Resize() }
        };
    }

    public IImageOperation GetOperation(OperationType name)
    {
        return _operations.TryGetValue(name, out IImageOperation operation) ? operation : null;
    }
}
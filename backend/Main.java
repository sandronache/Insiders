import app.App;

public class Main {
    public static void main(String[] args) {

        // fasad? pt logger un design pattern?

       // int stuff = App.MAX_NUMBER_OF_STUFF;
        //
        System.out.println("Main staring");
        App app = App.getInstance();
        app.run();
    }
}
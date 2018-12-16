package net.stevencai;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class App 
{

    public static void main( String[] args ) throws IOException, ClassNotFoundException {

        while(true) {
            try {
                Firebase.searchOpenCoursesAndRegister();

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}

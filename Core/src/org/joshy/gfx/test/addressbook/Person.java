package org.joshy.gfx.test.addressbook;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Jan 26, 2010
 * Time: 2:56:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class Person {
    public String first;
    public String last;
    public String homeNumber;
    public String mobileNumber;

    public Person(String first, String last, String homeNumber, String mobileNumber) {
        this.first = first;
        this.last = last;
        this.homeNumber = homeNumber;
        this.mobileNumber = mobileNumber;
    }

    @Override
    public String toString() {
        return "Person{" +
                "first='" + first + '\'' +
                ", last='" + last + '\'' +
                ", homeNumber='" + homeNumber + '\'' +
                ", mobileNumber='" + mobileNumber + '\'' +
                '}';
    }
}

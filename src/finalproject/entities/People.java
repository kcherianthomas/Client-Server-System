package finalproject.entities;

/* 
 * Intro to JAVA : FINAL PROJECT
 * Cherian Thomas
 * kct298@nyu.edu
 * 11 May 2020
 * 20 Hours
 * The project is to build a client/server system that reads data from a DB
 * into an object and sends the object to the server. The server then writes 
 * that data into its DB.
 */
public class People implements java.io.Serializable {

	private static final long serialVersionUID = 4190276780070819093L;

	private String first;
	private String last;
	private int age;
	private String city;
	private String id;

	public String getFirst() {
		return first;
	}

	public void setFirst(String first) {
		this.first = first;
	}

	public String getLast() {
		return last;
	}

	public void setLast(String last) {
		this.last = last;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "People [first=" + first + ", last=" + last + ", age=" + age + ", city=" + city + ", id=" + id + "]";
	}

}

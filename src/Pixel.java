
/** This class stores the Red, Green, Blue, and Alpha values in a 32 bit int.
 * It can also perform effects such as Black and White, and Negative.
 * @author 	Jerry Yu
 * @version	Oct. 23, 2012
 */

public class Pixel
{
	// Stores the Alpha, Red, Green and Blue values in a 32 bit int
	private int colour;
	
    /** Constructor - Creates a new Pixel object with given colour
     * @param value A 32 bit int representing the Alpha, Red, Green and Blue values
     */
	public Pixel(int value)
	{
		colour = value;
	}
	
	/** Copy Constructor - Creates a new Pixel object with same colour as given
			Pixel's colour
     * @param other The other Pixel object to copy
     */
	public Pixel (Pixel other)
	{
		colour = other.colour;
	}

	/** Changes this Pixel's colour to the new given colour
     * @param colour The new colour (represented by a 32 bit int)
     */
	public void setColour(int colour)
	{
		this.colour = colour;
	}
	
	/** Get this Pixel's colour (32 bit integer)
     * @return A 32 bit int represent this Pixel's Alpha, Red, Green & Blue values
     */
	public int getColour()
	{
		return colour;
	}
	
	/** Get an int array representing the colour of this Pixel
     * @return An int array of size 4, with the Alpha and RGB values with
	 *		indices 0-3 respectively.
     */
	public int [] getArrayFromPixels ()
	{
		int [] retArray = new int [4];
		retArray[0] = colour >>> 24;			// Shifts and set 24 MSB to 0
		retArray[1] = (colour >> 16) & 0xFF;	// The rest must be masked
		retArray[2] = (colour >>  8) & 0xFF;
		retArray[3] =  colour        & 0xFF;
		
		return retArray;
	}
	
	/** Get a formated String with information about this Pixel's colour
     * @return A String object with the Red, Green, Blue, and Alpha values
	 *		in that order. It also formats the String such that each value
	 *		is given 3 character spaces so a value of 55 would still take 
	 *		3 spaces (" 55")
     */
	public String toString()
	{
		int [] arr = getArrayFromPixels();
		return String.format("R: %3d, G: %3d, B: %3d, A: %3d", arr[1], arr[2], arr[3], arr[0]);
	}
	
	
	/*   	Begin Effects 		*/
	
	/** Changes this Pixel's colour so that the new RGB values are the same as
	 *		((255 + old value) mod 255). For speed, an XOR on only the 24 LSB
	 *		with FFFFFF gives the same result.
	 */
	public void negative ()
	{
		colour ^= 0xFFFFFF;
	}
	
	/** Changes this Pixel's colour so that the new RGB values are the 
	 *		weighted averages that result in a good black and white.
	 *		The weightings are: 0.3 * RED + 0.59 * GREEN + 0.11 * BLUE.
	 *		The sum of the weightings is then assigned to all 3 RGB values
	 *		http://www.imageprocessingbasics.com/rgb-to-grayscale/
	 */
	public void blackWhite ()
	{
		int [] arr = getArrayFromPixels();
		
		// Calculate new value and assign to the RGB values
		int newValue = (int) (0.3 * arr[1] + 0.59 * arr[2] + 0.11 * arr[3]);
		colour = Pixel.getPixelValue(arr[0], newValue, newValue, newValue);
	}
	
	
	/* 		Class Methods 		*/
	
	/** Converts the given Alpha, and RGB values into a 32 bit int
	 * @return a 32 bit int with 4 blocks of 8 bits representing the
	 *		Alpha, Red, Green, and Blue values from left to right
     */
	public static int getPixelValue (int alpha, int red, int green, int blue)
	{
		return alpha << 24 | red << 16 | green << 8 | blue;
	}
	
	/** Multiples each value in the second array by the weight and adds this
	 * 		result to the first array
	 * @param arr1 The array to add each result to
	 * @param arr2 The array where each element is multiplied and added to the first array
	 * @param weight The value to multiply the second array by before adding to the first
     */
	public static void addPixelArrays (int []arr1, int []arr2, int weight)
	{
		for (int i = 1; i < arr1.length; i ++)
			arr1[i] += arr2[i] * weight;
	}
	
	/** Calculates the average value of each element by dividing the value by the
			given weight. It assumes that alpha always has a value of 255.
			It then returns a 32 bit int representation of this new colour
	 * @param total The array that contains the total value of each colour
	 * @param weight The value to divide each element by
	 * @return A 32 bit representation, Alpha (always 255) and RGB in that order
     */
	public static int getAverageValue (int []total, int weight)
	{
		int value = 255;						// Assume the alpha will always be 255
		for (int i = 1; i < total.length; i ++)
		{
			int v = total[i] / weight;
			if (v > 255)						// Make sure no overflow
				v = 255;
			else if (v < 0)						// Make sure no underflow
				v = 0;
			value = value << 8 | v;				// Shift the bits over and insert value
		}
		
		return value;
	}
}



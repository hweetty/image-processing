image-processing
================

This was one of my grade 12 CS projects written in Java to manipulate images. The basic features includes *resize*, *sharpen*, *blur*, and *Black & White*. One bonus feature that I added was to detect and highlight shapes. It is able to recognize shapes like *rectanges*, *ovals*, *triangles*, and *trapezoids*.

I remember I tried several methods to determine the type of shape before succeeding. I actually almost missed the deadline to implement the basic features because I was trying to add my bonus feature.

##Recognizing Shapes
#####1) Find Outline
It determines the shape by figuring out the outline of each shape. (The shape must be of uniform colour). The program floodfills and stores the location of both the minimum and maximum horizontal and vertical pixel. It uses two arrays to store this information, one for horizontal data, the other for vertical. At each index of the horizontal array, the data stored would be the minimum and maximum y-coordinate, and vice-versa for the vertical array.

#####2) Profile shape
Then using this information, it tries to profile the shape. For example, rectangles have parallel lines, so the difference between the minimum and maximum values are fairly constant<sup>1</sup>. Trapezoids were a bit more tricky, but only have one pair of parallel lines. 

#####3) Flood fill :)


Here is a demo of my program in action: http://youtu.be/YaGbSSua9QE


<sup>1</sup> One thing to watch out for when scanning for parallel lines is that rectangles can be rotated such that initially, the distance between the min/max points are increasing. Then as the scan nears the middle of the rectangle, the distance decreases. Notice how a circle/oval also has this property.

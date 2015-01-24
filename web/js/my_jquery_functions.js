
$(document).ready(function(){

   // That first line (above) is just to wait until page is fully loaded.



   // jQuery methods go here...

	$("#headingArea").click(function(){
	    $(this).hide(1000, function(){$("#headingArea").show(1000);}  );
	});


	$("#myCanvas").click(function(event){
		//$("#headingArea").show();
		alert("You clicked in myCanvas! " + event.pageX+':'+event.pageY);
	}); 


/*

	$("#myCanvas").mousemove(function(event){
		  $("#textArea").text("X: " + event.pageX + ", Y: " + event.pageY);
 	}); 
*/

	i=0;
	$("input").keypress(function(event){
	    $("span").text(i+=1);

	    $("div").html("Key: " + event.which);
	});
/*

	$("input").keypress(function(event){

		var theTextBox = document.getElementById("textArea");
		theTextBox.value+=event.;
		theTextBox.scrollTop = theTextBox.scrollHeight;
	});
*/



}); 


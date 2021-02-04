function hide(identifiant) { 
      if (document.getElementById(identifiant).className == "hidden") { 
         document.getElementById(identifiant).className = "visible"; 
      } else { 
         document.getElementById(identifiant).className = "hidden"; 
      } 
   }
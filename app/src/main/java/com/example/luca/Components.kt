package com.example.luca

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.ui.theme.AppFont
import com.example.luca.ui.theme.LucaTheme
import com.example.luca.ui.theme.UIAccentYellow
import com.example.luca.ui.theme.UIBlack
import com.example.luca.ui.theme.UIWhite

@Composable
fun HeaderSection() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        color = UIWhite
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(painter = painterResource(id = R.drawable.ic_hamburger_sidebar),
                contentDescription = "Sidebar Icon",
                tint = UIBlack
            )

            Text(text = "Luca",
                style = AppFont.SemiBold,
                color = UIBlack,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp)

            Image(painter = painterResource(id = R.drawable.ic_luca_logo),
                contentDescription = "Luca Logo",
                Modifier.size(31.dp))
        }
    }
}

@Composable
fun FloatingNavbar() {
    Surface(
        modifier = Modifier
            .offset(y = (-23).dp)
            .width(225.dp)
            .height(75.dp),
        shape = RoundedCornerShape(50.dp),
        color = UIAccentYellow,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(59.dp),
                contentAlignment = Alignment.Center
            ){
                Icon(painter = painterResource(id = R.drawable.ic_scan_button),
                    contentDescription = "Scan Icon",
                    tint = UIBlack)
            }
            Box(
                modifier = Modifier
                    .size(59.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(30.dp),
                        clip = false
                    )
                    .background(
                        color = UIWhite,
                        shape = RoundedCornerShape(30.dp)
                    ),
                contentAlignment = Alignment.Center,
            ){
                Icon(painter = painterResource(id = R.drawable.ic_home_button),
                    contentDescription = "Home Icon",
                    tint = UIBlack)
            }
            Box(
                modifier = Modifier
                    .size(59.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_contacts_button),
                    contentDescription = "Contacts Icon",
                    tint = UIBlack
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(25.dp).width(100.dp))
}

@Preview
@Composable
fun ComponentsPreview(){
    LucaTheme {
        TemplateScreen()
    }
}
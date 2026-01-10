package com.example.luca

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
// import androidx.compose.ui.res.stringResource -> Sudah tidak dipakai
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.ui.theme.AppFont
import com.example.luca.ui.theme.LucaTheme
import com.example.luca.ui.theme.UIAccentYellow
import com.example.luca.ui.theme.UIBlack
import com.example.luca.ui.theme.UIDarkGrey
import com.example.luca.ui.theme.UIGrey
import com.example.luca.ui.theme.UIWhite

@Composable
fun HeaderSection() {
    Surface(
        color = UIWhite,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .height(50.dp)
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .background(UIWhite),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(painter = painterResource(id = R.drawable.ic_hamburger_sidebar),
                contentDescription = "Sidebar Icon",
                tint = UIBlack,
                modifier = Modifier.size(22.dp)
            )

            Text(text = "Luca",
                style = AppFont.SemiBold,
                color = UIBlack,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp)

            Image(painter = painterResource(id = R.drawable.ic_luca_logo),
                contentDescription = "Luca Logo",
                Modifier.size(26.dp))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputSection(
    label: String,
    value: String,
    placeholder: String,
    testTag: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            text = label,
            style = AppFont.SemiBold,
            fontSize = 16.sp,
            color = UIBlack,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(text = placeholder, color = UIDarkGrey)
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .testTag(testTag),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = UIWhite,
                unfocusedContainerColor = UIWhite,
                disabledContainerColor = UIWhite,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = UIBlack,
                unfocusedTextColor = UIBlack
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
        )
    }
}

@Composable
fun ParticipantItem(
    name: String,
    isAddButton: Boolean = false,
    isYou: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(60.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp) // Ukuran lingkaran luar (Container)
                .clip(CircleShape)
                .background(UIGrey)
                .testTag(if (isAddButton) "btn_add_participant" else "avatar_$name"),
            contentAlignment = Alignment.Center
        ) {
            if (isAddButton) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add_button),
                    contentDescription = "Add Participant",
                    tint = UIBlack,
                    // --- PERUBAHAN DI SINI ---
                    // Memperbesar ukuran Plus agar proporsional di dalam lingkaran
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Jika bukan tombol Add, tampilkan nama di bawahnya
        if (!isAddButton) {
            Text(
                text = name,
                fontSize = 12.sp,
                style = if (isYou) AppFont.SemiBold else AppFont.Regular,
                maxLines = 1,
                color = UIBlack
            )
        }
    }
}

@Preview
@Composable
fun ComponentsPreview(){
    LucaTheme {
        TemplateScreen()
    }
}
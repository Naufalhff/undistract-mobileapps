package com.example.undistract.features.add_behavior.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.undistract.R
import com.example.undistract.ui.components.AppSelector
import com.example.undistract.ui.components.BackButton
import com.example.undistract.ui.components.RestrictionNameInput

@Composable
fun AddRestrictionScreen(navController: NavController) {
    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // BACK BUTTON
        Row (
            modifier = Modifier
                .padding(start = 16.dp)
                .fillMaxWidth()
                .height(56.dp)
                .background(MaterialTheme.colorScheme.background),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BackButton (
                modifier = Modifier.size(24.dp),
                onClick = {}
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = stringResource(R.string.add_restriction),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // MAIN CONTENT
        Column (
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // APPS SELECTOR
            AppSelector(
                icon = Icons.Default.Star,
                title = stringResource(R.string.choose_apps_to_restrict),
                navController = navController,
                destinationRoute = "select_apps"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // QUESTION TO CHOOSE BOUNDARIES
            Row {
                Text(
                    stringResource(R.string.choose_what_restriction),
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // RESTRICTION FLEXBOX
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // FIRST LINE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FlexboxItem(
                        icon = Icons.Default.Star,
                        label = stringResource(R.string.block_permanently)
                    )

                    FlexboxItem(
                        icon = Icons.Default.Star,
                        label = stringResource(R.string.block_on_a_schedule)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECOND LINE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FlexboxItem(
                        icon = Icons.Default.Star,
                        label = stringResource(R.string.restrict_daily_usage)
                    )

                    FlexboxItem(
                        icon = Icons.Default.Star,
                        label = stringResource(R.string.apply_custom_session_restriction)
                    )
                }
            }

            RestrictionNameInput()
        }
    }
}

@Composable
fun FlexboxItem(
    icon: ImageVector,
    label: String
) {
    Box(
        modifier = Modifier
            .size(145.dp)
            .background(MaterialTheme.colorScheme.background)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.tertiary,
                shape = RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.undistract.ui.navigation.BottomNavItem
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.MyUsage,
        BottomNavItem.UsageLimit,
        BottomNavItem.ParentalControl,
        BottomNavItem.Profile,
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            val color = if (currentRoute == item.route) Color(92, 38, 161) else Color(153, 153, 153)

            NavigationBarItem(
                icon = { Icon(painterResource(item.icon), contentDescription = item.title, tint = color) },
                label = { Text(item.title, color = color) },
                modifier = Modifier.size(36.dp),
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

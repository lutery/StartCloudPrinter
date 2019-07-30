package cn.com.start.cloudprinter.startcloudprinter

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment

import kotlinx.android.synthetic.main.activity_dev.*

class DevActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dev)
        setSupportActionBar(toolbar)
    }

    override fun onSupportNavigateUp(): Boolean {

        var fragment: Fragment? = super.getSupportFragmentManager().findFragmentById(R.id.fragment);

        return NavHostFragment.findNavController(fragment!!).navigateUp();
    }

}

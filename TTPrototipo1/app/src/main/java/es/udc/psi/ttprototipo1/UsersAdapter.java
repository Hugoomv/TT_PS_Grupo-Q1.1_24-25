package es.udc.psi.ttprototipo1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class UsersAdapter extends BaseAdapter {

    ArrayList<User> connectedUsers;

    public UsersAdapter(ArrayList<User> users){
        connectedUsers = users;
    }

    @Override
    public int getCount() {
        return connectedUsers.size();
    }

    @Override
    public Object getItem(int position) {
        return connectedUsers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_users, parent, false);
        }

        // Obtener el usuario actual
        User user = connectedUsers.get(position);

        // Referencias a los TextView del layout
        TextView userNameTextView = convertView.findViewById(R.id.name_tex);
        TextView userEmailTextView = convertView.findViewById(R.id.email_tex);

        // Establecer los datos del usuario
        userNameTextView.setText(user.getName() != null ? user.getName() : "Nombre no disponible");
        userEmailTextView.setText(user.getEmail() != null ? user.getEmail() : "Email no disponible");

        return convertView;
    }
}

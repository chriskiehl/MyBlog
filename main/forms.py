from django import forms

class MyCoolForm(forms.Form):
  name = forms.CharField(label="name", max_length=100)
  age  = forms.CharField(label="age", max_length=100)
  stuff = forms.MultipleChoiceField(widget=forms.CheckboxSelectMultiple, choices=[(x,x) for x in range(5)])


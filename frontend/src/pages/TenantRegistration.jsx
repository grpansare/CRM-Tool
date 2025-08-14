import React, { useState } from "react";
import { useForm } from "react-hook-form";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { Eye, EyeOff, Check, ArrowLeft } from "lucide-react";
import { toast } from "react-hot-toast";
import api from "../services/api";

const TenantRegistration = () => {
  const { registerTenant } = useAuth();
  const navigate = useNavigate();
  const [showPassword, setShowPassword] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm();

  const onSubmit = async (data) => {
    setIsSubmitting(true);

    const registrationData = {
      tenantName: data.tenantName,
      companyName: data.companyName || data.tenantName,
      adminFirstName: data.adminFirstName,
      adminLastName: data.adminLastName,
      adminEmail: data.adminEmail,
      adminUsername: data.adminUsername || data.adminEmail.split("@")[0],
      adminPassword: data.adminPassword,
      companyAddress: data.companyAddress,
      companyPhone: data.companyPhone,
      companyEmail: data.companyEmail || data.adminEmail,
      industry: data.industry,
      timezone: data.timezone || "UTC",
      locale: data.locale || "en",
      subscriptionPlan: data.subscriptionPlan || "FREE",
      acceptTerms: true,
      acceptMarketing: data.acceptMarketing || false,
    };

    const result = await registerTenant(registrationData);
    setIsSubmitting(false);

    if (result.success) {
      navigate("/login");
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-12">
      <div className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="text-center mb-8">
          <Link
            to="/"
            className="inline-flex items-center text-primary-600 hover:text-primary-700 mb-4"
          >
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Home
          </Link>
          <h1 className="text-3xl font-bold text-gray-900 mb-2">
            Create Your Organization
          </h1>
          <p className="text-gray-600">
            Set up your CRM workspace and start managing customer relationships
          </p>
        </div>

        <div className="card">
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
            {/* Organization Information */}
            <div>
              <h2 className="text-xl font-semibold text-gray-900 mb-4">
                Organization Information
              </h2>

              <div className="grid md:grid-cols-2 gap-4">
                <div>
                  <label className="form-label">Organization Name *</label>
                  <input
                    type="text"
                    {...register("tenantName", {
                      required: "Organization name is required",
                      minLength: {
                        value: 2,
                        message: "Name must be at least 2 characters",
                      },
                    })}
                    className="input-field"
                    placeholder="Acme Corporation"
                  />
                  {errors.tenantName && (
                    <p className="error-text">{errors.tenantName.message}</p>
                  )}
                </div>

                <div>
                  <label className="form-label">Company Name *</label>
                  <input
                    type="text"
                    {...register("companyName", {
                      required: "Company name is required",
                      minLength: {
                        value: 2,
                        message: "Name must be at least 2 characters",
                      },
                    })}
                    className="input-field"
                    placeholder="Acme Corporation"
                  />
                  {errors.companyName && (
                    <p className="error-text">{errors.companyName.message}</p>
                  )}
                </div>
              </div>

              <div className="grid md:grid-cols-2 gap-4 mt-4">
                <div>
                  <label className="form-label">Company Email</label>
                  <input
                    type="email"
                    {...register("companyEmail", {
                      pattern: {
                        value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
                        message: "Invalid email format",
                      },
                    })}
                    className="input-field"
                    placeholder="info@acme.com"
                  />
                  {errors.companyEmail && (
                    <p className="error-text">{errors.companyEmail.message}</p>
                  )}
                </div>

                <div>
                  <label className="form-label">Company Phone</label>
                  <input
                    type="tel"
                    {...register("companyPhone")}
                    className="input-field"
                    placeholder="+1-555-0123"
                  />
                </div>
              </div>

              <div className="mt-4">
                <label className="form-label">Company Address</label>
                <textarea
                  {...register("companyAddress")}
                  className="input-field"
                  rows="2"
                  placeholder="123 Business St, City, State, ZIP"
                />
              </div>

              <div className="grid md:grid-cols-2 gap-4 mt-4">
                <div>
                  <label className="form-label">Industry</label>
                  <select {...register("industry")} className="input-field">
                    <option value="">Select Industry</option>
                    <option value="Technology">Technology</option>
                    <option value="Healthcare">Healthcare</option>
                    <option value="Finance">Finance</option>
                    <option value="Education">Education</option>
                    <option value="Retail">Retail</option>
                    <option value="Manufacturing">Manufacturing</option>
                    <option value="Other">Other</option>
                  </select>
                </div>

                <div>
                  <label className="form-label">Timezone</label>
                  <select {...register("timezone")} className="input-field">
                    <option value="UTC">UTC</option>
                    <option value="America/New_York">Eastern Time</option>
                    <option value="America/Chicago">Central Time</option>
                    <option value="America/Denver">Mountain Time</option>
                    <option value="America/Los_Angeles">Pacific Time</option>
                    <option value="Europe/London">London</option>
                    <option value="Europe/Paris">Paris</option>
                    <option value="Asia/Tokyo">Tokyo</option>
                  </select>
                </div>
              </div>
            </div>

            {/* Admin User Information */}
            <div>
              <h2 className="text-xl font-semibold text-gray-900 mb-4">
                Admin Account
              </h2>

              <div className="grid md:grid-cols-2 gap-4">
                <div>
                  <label className="form-label">First Name *</label>
                  <input
                    type="text"
                    {...register("adminFirstName", {
                      required: "First name is required",
                      minLength: {
                        value: 2,
                        message: "Name must be at least 2 characters",
                      },
                    })}
                    className="input-field"
                    placeholder="John"
                  />
                  {errors.adminFirstName && (
                    <p className="error-text">
                      {errors.adminFirstName.message}
                    </p>
                  )}
                </div>

                <div>
                  <label className="form-label">Last Name *</label>
                  <input
                    type="text"
                    {...register("adminLastName", {
                      required: "Last name is required",
                      minLength: {
                        value: 2,
                        message: "Name must be at least 2 characters",
                      },
                    })}
                    className="input-field"
                    placeholder="Doe"
                  />
                  {errors.adminLastName && (
                    <p className="error-text">{errors.adminLastName.message}</p>
                  )}
                </div>
              </div>

              <div className="grid md:grid-cols-2 gap-4 mt-4">
                <div>
                  <label className="form-label">Email *</label>
                  <input
                    type="email"
                    {...register("adminEmail", {
                      required: "Email is required",
                      pattern: {
                        value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
                        message: "Invalid email format",
                      },
                    })}
                    className="input-field"
                    placeholder="john.doe@acme.com"
                  />
                  {errors.adminEmail && (
                    <p className="error-text">{errors.adminEmail.message}</p>
                  )}
                </div>

                <div>
                  <label className="form-label">Username *</label>
                  <input
                    type="text"
                    {...register("adminUsername", {
                      required: "Username is required",
                      pattern: {
                        value: /^[a-zA-Z0-9_]+$/,
                        message:
                          "Username can only contain letters, numbers, and underscores",
                      },
                      minLength: {
                        value: 3,
                        message: "Username must be at least 3 characters",
                      },
                    })}
                    className="input-field"
                    placeholder="johndoe"
                  />
                  {errors.adminUsername && (
                    <p className="error-text">{errors.adminUsername.message}</p>
                  )}
                </div>
              </div>

              <div className="mt-4">
                <label className="form-label">Password *</label>
                <div className="relative">
                  <input
                    type={showPassword ? "text" : "password"}
                    {...register("adminPassword", {
                      required: "Password is required",
                      minLength: {
                        value: 8,
                        message: "Password must be at least 8 characters",
                      },
                      pattern: {
                        value:
                          /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/,
                        message:
                          "Password must contain uppercase, lowercase, number, and special character",
                      },
                    })}
                    className="input-field pr-10"
                    placeholder="SecurePass123!"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500 hover:text-gray-700"
                  >
                    {showPassword ? (
                      <EyeOff className="h-4 w-4" />
                    ) : (
                      <Eye className="h-4 w-4" />
                    )}
                  </button>
                </div>
                {errors.adminPassword && (
                  <p className="error-text">{errors.adminPassword.message}</p>
                )}
              </div>
            </div>

            {/* Subscription Plan */}
            <div>
              <h2 className="text-xl font-semibold text-gray-900 mb-4">
                Subscription Plan
              </h2>
              <div className="grid md:grid-cols-2 gap-4">
                <div className="card border-2 border-primary-500">
                  <div className="flex items-center justify-between mb-2">
                    <h3 className="font-semibold">Free Plan</h3>
                    <span className="text-primary-600 font-bold">$0/month</span>
                  </div>
                  <ul className="text-sm text-gray-600 space-y-1 mb-4">
                    <li>• 5 Users</li>
                    <li>• Basic CRM</li>
                    <li>• Email Support</li>
                  </ul>
                  <input
                    type="radio"
                    {...register("subscriptionPlan")}
                    value="FREE"
                    defaultChecked
                    className="mr-2"
                  />
                  <label className="text-sm">Select Free Plan</label>
                </div>

                <div className="card">
                  <div className="flex items-center justify-between mb-2">
                    <h3 className="font-semibold">Starter Plan</h3>
                    <span className="text-primary-600 font-bold">
                      $29/month
                    </span>
                  </div>
                  <ul className="text-sm text-gray-600 space-y-1 mb-4">
                    <li>• 25 Users</li>
                    <li>• Full CRM</li>
                    <li>• Priority Support</li>
                    <li>• Basic Reports</li>
                  </ul>
                  <input
                    type="radio"
                    {...register("subscriptionPlan")}
                    value="STARTER"
                    className="mr-2"
                  />
                  <label className="text-sm">Select Starter Plan</label>
                </div>
              </div>
            </div>

            {/* Terms and Conditions */}
            <div className="space-y-4">
              <div className="flex items-start">
                <input
                  type="checkbox"
                  {...register("acceptTerms", {
                    required: "You must accept the terms and conditions",
                  })}
                  className="mt-1 mr-3"
                />
                <label className="text-sm text-gray-700">
                  I agree to the{" "}
                  <a
                    href="#"
                    className="text-primary-600 hover:text-primary-700 underline"
                  >
                    Terms of Service
                  </a>{" "}
                  and{" "}
                  <a
                    href="#"
                    className="text-primary-600 hover:text-primary-700 underline"
                  >
                    Privacy Policy
                  </a>
                </label>
              </div>
              {errors.acceptTerms && (
                <p className="error-text">{errors.acceptTerms.message}</p>
              )}

              <div className="flex items-start">
                <input
                  type="checkbox"
                  {...register("acceptMarketing")}
                  className="mt-1 mr-3"
                />
                <label className="text-sm text-gray-700">
                  I would like to receive marketing communications about product
                  updates and features
                </label>
              </div>
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={isSubmitting}
              className="btn-primary w-full py-3 text-lg disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isSubmitting
                ? "Creating Organization..."
                : "Create Organization"}
            </button>

            <p className="text-center text-sm text-gray-600">
              Already have an account?{" "}
              <Link
                to="/login"
                className="text-primary-600 hover:text-primary-700 font-medium"
              >
                Sign in here
              </Link>
            </p>
          </form>
        </div>
      </div>
    </div>
  );
};

export default TenantRegistration;
